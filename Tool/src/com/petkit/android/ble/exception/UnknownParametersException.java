/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 * 
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.petkit.android.ble.exception;

import com.petkit.android.utils.LogcatStorageHelper;

public class UnknownParametersException extends Exception {

	private static final long serialVersionUID = -1531229400491481208L;
	
	private final int mOpCode;
	private final String[] mParameters;

	public UnknownParametersException(final String message, final int curKey, final String... params) {
		super(message);

		mParameters = params;
		mOpCode = curKey;
	}

	@Override
	public String getMessage() {
		String message = String.format("%s (OpKey: %s, Parameters: %s..)", super.getMessage(), mOpCode, (mParameters == null ? "" : mParameters.toString()));
		LogcatStorageHelper.addLog(message);
		return message;
	}

}
