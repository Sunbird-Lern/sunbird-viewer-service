package org.sunbird.viewer.core

object Exceptions {
}

case class ServerException(message: String, cause:Throwable = null) extends Exception(message, cause)

case class ClientException(message: String, cause:Throwable = null) extends Exception(message, cause)
