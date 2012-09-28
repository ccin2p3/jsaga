/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Luigi Zangrando (zangrando@pd.infn.it)
 */


package org.glite.ce.commonj.utils;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;


/**
 * A class loader for loading jar files, both local and remote.
 * @author Luigi Zangrando (zangrando@pd.infn.it)
 */
public class JarClassLoader extends URLClassLoader {

   /**
    * Creates a new JarClassLoader for the specified url.
    *
    * @param url the url of the jar file
    */
   public JarClassLoader( String href, ClassLoader parent ) throws MalformedURLException {
      this( new URL( href ), parent );
   }


   public JarClassLoader( URL url, ClassLoader parent ) {
      super( new URL[] { url }, parent );
   }


   public void addJarURL( URL url ) throws MalformedURLException {
      addURL( url );
   }


   public void addJarURL( String href ) throws MalformedURLException {
      if( href != null ) {
         addURL( new URL( href ) );
      }
   }


   /**
    * Returns the name of the jar file main class, or null if
    * no "Main-Class" manifest attributes was defined.
    * @param sensorURL
    */
   public String getMainClassName( URL sensorURL ) throws IOException {
      URL u = new URL( "jar", "", sensorURL + "!/" );
      JarURLConnection uc = ( JarURLConnection )u.openConnection( );
      Attributes attr = uc.getMainAttributes( );

      return attr != null ? attr.getValue( Attributes.Name.MAIN_CLASS ) : null;
   }


   /**
    * Invokes the application in this jar file given the name of the
    * main class and an array of arguments. The class must define a
    * static method "main" which takes an array of String arguemtns
    * and is of return type "void".
    *
    * @param name the name of the main class
    * @param args the arguments for the application
    * @exception ClassNotFoundException if the specified class could not be found
    * @exception NoSuchMethodException if the specified class does not contain a "main" method
    * @exception InvocationTargetException if the application raised an exception
    */
   public void invokeClass( String name, String[] args ) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
      Class c = loadClass( name );
      Method m = c.getMethod( "main", new Class[] { args.getClass( ) } );
      m.setAccessible( true );
      
      int mods = m.getModifiers( );

      if( m.getReturnType( ) != void.class || !Modifier.isStatic( mods ) || !Modifier.isPublic( mods ) ) {
         throw new NoSuchMethodException( "main" );
      }

      try {
         m.invoke( null, new Object[] { args } );
      } catch( IllegalAccessException e ) {// This should not happen, as we have disabled access checks
      }
   }

}
