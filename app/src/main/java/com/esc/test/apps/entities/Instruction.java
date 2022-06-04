package com.esc.test.apps.entities;

import static com.esc.test.apps.utils.TutAction.NEXT;
import static com.esc.test.apps.utils.TutAction.WAIT;

import com.esc.test.apps.utils.TutAction;

public class Instruction {

    private final String prompt;
    private final TutAction action;
    private final String pos;
    private final String altPos;
    private final boolean isPlayer;

    public Instruction(String prompt, TutAction action, String pos) {
        this.prompt = prompt;
        this.action = action;
        this.pos = pos;
        this.isPlayer = false;
        altPos = null;
    }

    public Instruction(String pos) {
        this.prompt = null;
        this.action = NEXT;
        this.pos = pos;
        this.isPlayer = false;
        altPos = null;
    }

    public Instruction(String prompt, TutAction action, String pos, String altPos, boolean isPlayer) {
        this.prompt = prompt;
        this.action = action;
        this.pos = pos;
        this.altPos = altPos;
        this.isPlayer = isPlayer;
    }

    public String getPrompt() {
        return prompt;
    }

    public TutAction getAction() {
        return action;
    }

    public String getPos() {
        return pos;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public String getAltPos() {
        return altPos;
    }
}

