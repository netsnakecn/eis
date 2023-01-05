package com.maicard.core.constants;

public enum DisplayLevel {
	system(100),
	platform(90),
	partner(80),
	user(70),
	login(60),
	subscriber(50), 
	open(0);
	
	public int weight;
	
	private DisplayLevel(int weight){
		this.weight = weight;
	}

	public static int findByName(String displayLevel) {
		for(DisplayLevel dl : DisplayLevel.values()) {
			if(dl.name().equalsIgnoreCase(displayLevel)) {
				return dl.weight;
			}
		}
		return 0;
	}

}
