package com.lucid.core.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import jakarta.validation.ConstraintViolation;

@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error", visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class LRApiError {
	private HttpStatus status;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;
	private String message;
	private String debugMessage;
	private List<LRApiSubError> subErrors;

	private LRApiError() {
		timestamp = LocalDateTime.now();
	}

	public LRApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	public LRApiError(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.message = "Unexpected error";
		this.debugMessage = ex.getLocalizedMessage();
	}

	public LRApiError(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.message = message;
		this.debugMessage = ex.getLocalizedMessage();
	}

	private void addSubError(LRApiSubError subError) {
		if (subErrors == null) {
			subErrors = new ArrayList<>();
		}
		subErrors.add(subError);
	}

	public void addValidationError(String object, String field, Object rejectedValue, String message) {
		addSubError(new LRApiValidationError(object, field, rejectedValue, message));
	}

	private void addValidationError(String object, String message) {
		addSubError(new LRApiValidationError(object, message));
	}

	private void addValidationError(FieldError fieldError) {
		this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(),
				fieldError.getDefaultMessage());
	}

	public void addValidationErrors(List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	private void addValidationError(ObjectError objectError) {
		this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
	}

	public void addValidationError(List<ObjectError> globalErrors) {
		globalErrors.forEach(this::addValidationError);
	}

	/**
	 * Utility method for adding error of ConstraintViolation. Usually when
	 * a @Validated validation fails.
	 *
	 * @param cv the ConstraintViolation
	 */
	private void addValidationError(ConstraintViolation<?> cv) {
		this.addValidationError(cv.getRootBeanClass().getSimpleName(),
				((PathImpl) cv.getPropertyPath()).getLeafNode().asString(), cv.getInvalidValue(), cv.getMessage());
	}

	public void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
		constraintViolations.forEach(this::addValidationError);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDebugMessage() {
		return debugMessage;
	}

	public void setDebugMessage(String debugMessage) {
		this.debugMessage = debugMessage;
	}

	public List<LRApiSubError> getSubErrors() {
		return subErrors;
	}

	public void setSubErrors(List<LRApiSubError> subErrors) {
		this.subErrors = subErrors;
	}

}
