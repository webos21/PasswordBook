package com.gmail.webos21.passwordbook.keypad;

public enum KeypadButton {
    /* Indent */ ZERO("0", KeypadButtonCategory.NUMBER),
    /* Indent */ ONE("1", KeypadButtonCategory.NUMBER),
    /* Indent */ TWO("2", KeypadButtonCategory.NUMBER),
    /* Indent */ THREE("3", KeypadButtonCategory.NUMBER),
    /* Indent */ FOUR("4", KeypadButtonCategory.NUMBER),
    /* Indent */ FIVE("5", KeypadButtonCategory.NUMBER),
    /* Indent */ SIX("6", KeypadButtonCategory.NUMBER),
    /* Indent */ SEVEN("7", KeypadButtonCategory.NUMBER),
    /* Indent */ EIGHT("8", KeypadButtonCategory.NUMBER),
    /* Indent */ NINE("9", KeypadButtonCategory.NUMBER),
    /* Indent */ BACKSPACE("\u2190", KeypadButtonCategory.OPERATOR),
    /* Indent */ DUMMY("", KeypadButtonCategory.DUMMY);

    CharSequence mText; // Display Text
    KeypadButtonCategory mCategory;

    KeypadButton(CharSequence text, KeypadButtonCategory category) {
        mText = text;
        mCategory = category;
    }

    public CharSequence getText() {
        return mText;
    }
}
