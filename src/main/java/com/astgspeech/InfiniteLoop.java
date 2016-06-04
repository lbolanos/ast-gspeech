package com.astgspeech;

public class InfiniteLoop {

	private boolean infinite = true;

	public InfiniteLoop(boolean infinite) {
		super();
		this.setInfinite(infinite);
	}

	public boolean isInfinite() {
		return infinite;
	}

	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}
	
	
}
