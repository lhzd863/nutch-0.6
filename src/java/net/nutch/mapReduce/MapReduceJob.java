/* Copyright (c) 2005 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.mapReduce;

import java.io.IOException;
import java.io.File;

/** Specifies a map/reduce job.  This names the {@link Mapper}, combiner (if
 * any), {@link Partitioner}, {@link Reducer}, {@link InputFormat}, and {@link
 * OutputFormat} implementations to be used.  It also indicates the set of
 * input files, and where the output files should be written.
 */
public class MapReduceJob {
  private File[] inputFiles;
  private InputFormat inputFormat;
  
  private File outputBase;
  private OutputFormat outputFormat;
  
  private Class mapperClass =  DefaultMapper.class;
  private Class partitionerClass = DefaultPartitioner.class;
  private Class reducerClass =  DefaultReducer.class;
  private Class combinerClass = null;
  
  private int numMapTasks = 10;
  private int numReduceTasks = 10;

  /** Constructs a map/reduce job.
   *
   * @param inputBase the base name for input file enumeration
   * @param inputFormat the name of the input file format
   * @param outputBase the base name for output file name generation
   * @param outputFormat the name of the output file format
   */
  public MapReduceJob(File inputBase, String inputFormat,
                      File outputBase, String outputFormat)
    throws IOException {

    throw new RuntimeException("not yet implemented");
  }

  /** Set the {@link Mapper} class. */
  public void setMapper(Class mapperClass) {
    if (!Mapper.class.isAssignableFrom(mapperClass))
      throw new IllegalArgumentException(mapperClass+" not a Mapper");
    this.mapperClass = mapperClass;
  }

  /** Set the {@link Reducer} class. */
  public void setReducer(Class reducerClass) {
    if (!Reducer.class.isAssignableFrom(reducerClass))
      throw new IllegalArgumentException(reducerClass+" not a Reducer");
    this.reducerClass = reducerClass;
  }

  /** Set the {@link Partitioner} class. */
  public void setPartitioner(Class partitionerClass) {
    if (!Partitioner.class.isAssignableFrom(partitionerClass))
      throw new IllegalArgumentException(partitionerClass+" not a Partitioner");
    this.partitionerClass = partitionerClass;
  }

  /** Set the combiner class, if any, to a {@link Reducer}.  A combiner can be
   * specified to optimize the system.  This is appropriate when the {@link
   * Reducer} is commutative and associative.  The combiner is used to
   * partially reduce intermediate values prior to invoking the {@link
   * Reducer}.
   */
  public void setCombiner(Class combinerClass) {
    if (!Reducer.class.isAssignableFrom(combinerClass))
      throw new IllegalArgumentException(combinerClass+" not a Reducer");
    this.combinerClass = combinerClass;
  }

  /** Set the desired number of map tasks to be executed.*/
  public void setNumMapTasks(int numMapTasks) {
    this.numMapTasks = numMapTasks;
  }

  /** Set the desired number of reduce tasks to be executed.*/
  public void setNumReduceTasks(int numReduceTasks) {
    this.numReduceTasks = numReduceTasks;
  }

}

