<%@ page 
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="javax.servlet.*"
  import="javax.servlet.http.*"
  import="java.io.*"
  import="java.util.*"
  import="java.net.*"

  import="net.nutch.html.Entities"
  import="net.nutch.searcher.*"
  import="net.nutch.plugin.*"
  import="net.nutch.clustering.*"
  import="net.nutch.util.NutchConf"

%><%!
  // NOTE: Dawid Weiss
  // these are servlet's class variables. Merely to speed up access
  // to them (instead of referencing static objects).

  /**
   * Number of hits to retrieve and cluster if clustering extension is available
   * and clustering is on. By default, 100. Configurable via nutch-conf.xml.
   */
  private final static int HITS_TO_CLUSTER =
    NutchConf.getInt("extension.clustering.hits-to-cluster", 100);

  /**
   * An instance of the clustering extension, if available.
   */
  private static OnlineClusterer clusterer;

  static {
    try {
      clusterer = OnlineClustererFactory.getOnlineClusterer();
    } catch (PluginRuntimeException e) {
      // NOTE: Dawid Weiss
      // should we ignore plugin exceptions, or rethrow it? Rethrowing
      // it effectively prevents the servlet class from being loaded into
      // the JVM
    }
  }

%>

<%--
// Uncomment this to enable query refinement.
// Do the same to "refine-query.jsp" below.
<%@ include file="./refine-query-init.jsp" %>
--%>

<%
  NutchBean bean = NutchBean.get(application);
  // set the character encoding to use when interpreting request values 
  request.setCharacterEncoding("UTF-8");

  bean.LOG.info("query request from " + request.getRemoteAddr());

  // get query from request
  String queryString = request.getParameter("query");
  if (queryString == null)
    queryString = "";
  String htmlQueryString = Entities.encode(queryString);

  // a flag to make the code cleaner a bit.
  boolean clusteringAvailable = (clusterer != null);

  String clustering = "";
  if (clusteringAvailable && "yes".equals(request.getParameter("clustering")))
    clustering = "yes";

  int start = 0;          // first hit to display
  String startString = request.getParameter("start");
  if (startString != null)
    start = Integer.parseInt(startString);

  int hitsPerPage = 10;          // number of hits to display
  String hitsString = request.getParameter("hitsPerPage");
  if (hitsString != null)
    hitsPerPage = Integer.parseInt(hitsString);

  int hitsPerSite = 2;                            // max hits per site
  String hitsPerSiteString = request.getParameter("hitsPerSite");
  if (hitsPerSiteString != null)
    hitsPerSite = Integer.parseInt(hitsPerSiteString);

  int hitsToCluster = HITS_TO_CLUSTER;            // number of hits to cluster

  Query query = Query.parse(queryString);
  bean.LOG.info("query: " + queryString);

  String language =
    ResourceBundle.getBundle("org.nutch.jsp.search", request.getLocale())
    .getLocale().getLanguage();
  String requestURI = HttpUtils.getRequestURL(request).toString();
  String base = requestURI.substring(0, requestURI.lastIndexOf('/'));

%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
  // To prevent the character encoding declared with 'contentType' page
  // directive from being overriden by JSTL (apache i18n), we freeze it
  // by flushing the output buffer. 
  // see http://java.sun.com/developer/technicalArticles/Intl/MultilingualJSP/
  out.flush();
%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n" prefix="i18n" %>
<i18n:bundle baseName="org.nutch.jsp.search"/>
<html lang="<%= language %>">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<head>
<title>Nutch: <i18n:message key="title"/></title>
<link rel="icon" href="/img/favicon.ico" type="image/x-icon"/>
<link rel="shortcut icon" href="/img/favicon.ico" type="image/x-icon"/>
<jsp:include page="/include/style.html"/>
<base href="<%= base  + "/" + language %>/">
</head>

<body>

<jsp:include page="<%= language + "/include/header.html"%>"/>

 <form name="search" action="/search.jsp" method="get">
 <input name="query" size=44 value="<%=htmlQueryString%>">
 <input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>">
 <input type="submit" value="<i18n:message key="search"/>">
 <% if (clusteringAvailable) { %>
   <input id="clustbox" type="checkbox" name="clustering" value="yes" <% if (clustering.equals("yes")) { %>CHECKED<% } %>>
    <label for="clustbox"><i18n:message key="clustering"/></label>
 <% } %>
 </form>

<%--
// Uncomment this to enable query refinement.
// Do the same to "refine-query-init.jsp" above.
<%@ include file="./refine-query.jsp" %>
--%>

<%
   // how many hits to retrieve? if clustering is on and available,
   // take "hitsToCluster", otherwise just get hitsPerPage
   int hitsToRetrieve = (clusteringAvailable && clustering.equals("yes") ? hitsToCluster : hitsPerPage);

   if (clusteringAvailable && clustering.equals("yes")) {
     bean.LOG.info("Clustering is on, hits to retrieve: " + hitsToRetrieve);
   }

   // perform query
    // NOTE by Dawid Weiss:
    // The 'clustering' window actually moves with the start
    // position.... this is good, bad?... ugly?....
   Hits hits = bean.search(query, start + hitsToRetrieve, hitsPerSite);
   int end = (int)Math.min(hits.getLength(), start + hitsPerPage);
   int length = end-start;
   int realEnd = (int)Math.min(hits.getLength(), start + hitsToRetrieve);

   Hit[] show = hits.getHits(start, realEnd-start);
   HitDetails[] details = bean.getDetails(show);
   String[] summaries = bean.getSummary(details, query);

   bean.LOG.info("total hits: " + hits.getTotal());
%>

<i18n:message key="hits">
  <i18n:messageArg value="<%=new Long((end==0)?0:(start+1))%>"/>
  <i18n:messageArg value="<%=new Long(end)%>"/>
  <i18n:messageArg value="<%=new Long(hits.getTotal())%>"/>
</i18n:message>

<%
// be responsive
out.flush();
%>

<br><br>

<% if (clustering.equals("yes") && length != 0) { %>
<table border=0 cellspacing="3" cellpadding="0">

<tr>

<td valign="top">

<% } %>

<%
  for (int i = 0; i < length; i++) {      // display the hits
    Hit hit = show[i];
    HitDetails detail = details[i];
    String title = detail.getValue("title");
    String url = detail.getValue("url");
    String summary = summaries[i];
    String id = "idx=" + hit.getIndexNo() + "&id=" + hit.getIndexDocNo();

    if (title == null || title.equals(""))        // use url for docs w/o title
      title = url;
    %>
    <b><a href="<%=url%>"><%=Entities.encode(title)%></a></b>
    <%@ include file="./more.jsp" %>
    <% if (!"".equals(summary)) { %>
    <br><%=summary%>
    <% } %>
    <br>
    <span class="url"><%=Entities.encode(url)%></span>
    (<a href="/cached.jsp?<%=id%>"><i18n:message key="cached"/></a>)
    (<a href="/explain.jsp?<%=id%>&query=<%=URLEncoder.encode(queryString)%>"><i18n:message key="explain"/></a>)
    (<a href="/anchors.jsp?<%=id%>"><i18n:message key="anchors"/></a>)
    <% if (hit.moreFromSiteExcluded()) {
    String more =
    "query="+URLEncoder.encode("site:"+hit.getSite()+" "+queryString)
    +"&start="+start+"&hitsPerPage="+hitsPerPage+"&hitsPerSite="+0
    +"&clustering="+clustering;%>
    (<a href="/search.jsp?<%=more%>"><i18n:message key="moreFrom"/>
     <%=hit.getSite()%></a>)
    <% } %>
    <br><br>
<% } %>

<% if (clustering.equals("yes") && length != 0) { %>

</td>

<!-- clusters -->
<td style="border-right: 1px dotted gray;" />&#160;</td>
<td align="left" valign="top" width="25%">
<%@ include file="./cluster.jsp" %>
</td>

</tr>
</table>

<% } %>

<%

if ((hits.totalIsExact() && end < hits.getTotal()) // more hits to show
    || (!hits.totalIsExact() && (hits.getLength() > start+hitsPerPage))) {
%>
    <form name="search" action="<%= base %>/search.jsp" method="get">
    <input type="hidden" name="query" value="<%=htmlQueryString%>">
    <input type="hidden" name="start" value="<%=end%>">
    <input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>">
    <input type="hidden" name="hitsPerSite" value="<%=hitsPerSite%>">
    <input type="hidden" name="clustering" value="<%=clustering%>">
    <input type="submit" value="<i18n:message key="next"/>">
    </form>
<%
    }

if ((!hits.totalIsExact() && (hits.getLength() <= start+hitsPerPage))) {
%>
    <form name="search" action="/search.jsp" method="get">
    <input type="hidden" name="query" value="<%=htmlQueryString%>">
    <input type="hidden" name="hitsPerPage" value="<%=hitsPerPage%>">
    <input type="hidden" name="hitsPerSite" value="0">
    <input type="hidden" name="clustering" value="<%=clustering%>">
    <input type="submit" value="<i18n:message key="showAllHits"/>">
    </form>
<%
    }
%>

<p>
<a href="http://www.nutch.org/">
<img border="0" src="/img/poweredbynutch_01.gif">
</a>

<jsp:include page="/include/footer.html"/>

</body>
</html>
