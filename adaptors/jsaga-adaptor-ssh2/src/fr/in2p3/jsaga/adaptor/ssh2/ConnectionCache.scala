/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.in2p3.jsaga.adaptor.ssh2

import ch.ethz.ssh2.Connection
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import scala.collection.mutable.HashMap

class ConnectionCache(val connectionKeepAlive: Long) { cache =>

  class ConnectionInfo(val connection: Connection) {
    var lastConnectionUse: Long = System.currentTimeMillis
    var used = 0
    
    def get = {
      used += 1
      connection
    }
    
    def release = {
      used -= 1
      lastConnectionUse = System.currentTimeMillis
    }
    
    def recentlyUsed = 
      used > 0 || lastConnectionUse + connectionKeepAlive < System.currentTimeMillis 
    
  }
  
  private val connectionCloser = Executors.newSingleThreadScheduledExecutor
  private val connections = new HashMap[(String, String, Int), ConnectionInfo]
  
  def adaptorKey(adaptor: SSHAdaptor) = (adaptor.user, adaptor.host, adaptor.port)
  
  def cached(adaptor: SSHAdaptor): Connection = synchronized {
    connections.getOrElseUpdate(adaptorKey(adaptor), new ConnectionInfo(adaptor.openConnection)).get
  }
  
  def release(adaptor: SSHAdaptor) = synchronized {
    val key = adaptorKey(adaptor)
    connections(key).release
    connectionCloser.schedule(
        new Runnable {
          def run = cache.synchronized {
            connections.get(key) match {
              case Some(c) if(!c.recentlyUsed) => 
                c.connection.close
                connections.remove(key)
              case _ =>
            }
          }
        },
        (connectionKeepAlive * 1.05).toLong,
        TimeUnit.MILLISECONDS
      )
  }
  
}
