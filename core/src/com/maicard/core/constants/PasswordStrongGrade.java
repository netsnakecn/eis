package com.maicard.core.constants;

public enum PasswordStrongGrade {
	NONE(0),
	CAPITIAL_AND_SMALL_LETTER(1),
	LETTER_AND_NUM(2),
	WORD_AND_SYMBOL(3);
	
	public final int id;
	
	private PasswordStrongGrade(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}


}
