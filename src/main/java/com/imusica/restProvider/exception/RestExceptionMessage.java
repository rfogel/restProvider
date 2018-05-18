package com.imusica.restProvider.exception;

public interface RestExceptionMessage
{
	String STATUS_NOK = "Request finalizado com falha. Status: %s";
	String ERROR_CLOSING_CONNECTION = "Erro ao finalizar conexão: %s";
}
