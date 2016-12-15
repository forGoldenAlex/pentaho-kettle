package org.pentaho.di.engine.kettlenative.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EngineITest {

  TransMeta testMeta;
  Engine engine = new Engine();

  @Before
  public void before() throws KettleException {
    KettleEnvironment.init();
    testMeta = new TransMeta( getClass().getClassLoader().getResource( "test2.ktr" ).getFile() );
  }

  @Test
  public void testExec() throws KettleXMLException, KettleMissingPluginsException, InterruptedException {
    TransMeta meta = new TransMeta( getClass().getClassLoader().getResource( "lorem.ktr" ).getFile() );
    ITransformation trans = Transformation.convert( meta );

    IEngine engine = new Engine();
    engine.execute( trans );
  }

  @Test
  public void test2Sources1Sink()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "2InputsWithConsistentColumns.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    assertThat( reports.size(), is( 3 ) );
    IExecutableOperation dataGrid1 = getByName( "Data Grid", reports );
    IExecutableOperation dataGrid2 = getByName( "Data Grid 2", reports );
    IExecutableOperation dummy = getByName( "Dummy (do nothing)", reports );
    assertThat( dataGrid1.getOut(), is( 1 ) );
    assertThat( dataGrid2.getOut(), is( 1 ) );
    assertThat( "dummy should get rows fromm both data grids", dummy.getIn(), is( 2 ) );
    System.out.println( reports );
  }

  @Test
  public void test1source2trans1sink()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "1source.2Trans.1sink.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    assertThat( reports.size(), is( 5 ) );
    System.out.println( reports );

  }

  @Test
  public void simpleFilter()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "simpleFilter.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    System.out.println( reports );

  }

  @Test
  public void testLookup()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "SparkSample.ktr" );
    List<IProgressReporting<IDataEvent>> reports = result.getDataEventReport();
    assertTrue( new File( getClass().getClassLoader().getResource( "Output.txt" ).getFile()).length() > 0 );
  }


  private IExecutionResult getTestExecutionResult( String transName )
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException,
    ExecutionException {
    TransMeta meta = new TransMeta( getClass().getClassLoader().getResource( transName ).getFile() );
    ITransformation trans = Transformation.convert( meta );
    IExecutionResultFuture resultFuture = engine.execute( trans );
    return resultFuture.get();
  }

  private IExecutableOperation getByName( String name, List<IProgressReporting<IDataEvent>> reports ) {
    return reports.stream()
      .map( IExecutableOperation.class::cast )
      .filter( report -> report.getId().equals( name ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException() );
  }


}