package com.urbanbike.exception;

public class RentalAlreadyFinishedException extends RuntimeException {

	public RentalAlreadyFinishedException(String message) {
		super(message);
	}

}
