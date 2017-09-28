package com.pccw.common;

@FunctionalInterface
public interface InnerFunction<T> {
	T execute() throws Exception;
}
