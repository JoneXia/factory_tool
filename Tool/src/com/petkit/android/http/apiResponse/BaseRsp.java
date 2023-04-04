package com.petkit.android.http.apiResponse;

public class BaseRsp {

	private ErrorInfor error;
	
	
	public BaseRsp() {
		super();
	}


	public BaseRsp(int code, String msg) {
		super();
		this.error = new ErrorInfor(code, msg);
	}


	public ErrorInfor getError() {
		return error;
	}


	public void setError(ErrorInfor error) {
		this.error = error;
	}


	public class ErrorInfor{
		
		private int code;
		private String msg;
		
		public ErrorInfor(int code, String msg) {
			super();
			this.code = code;
			this.msg = msg;
		}
		
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
	}
}
