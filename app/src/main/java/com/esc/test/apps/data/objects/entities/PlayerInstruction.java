package com.esc.test.apps.data.objects.entities;

import com.esc.test.apps.common.utils.TutAction;

public class PlayerInstruction {

    private final String prompt;
    private final TutAction action;
    private final String pos;
    private final String altPos;

    public PlayerInstruction(String prompt, TutAction action, String pos) {
        this.prompt = prompt;
        this.action = action;
        this.pos = pos;
        altPos = null;
    }

    public PlayerInstruction(String prompt, TutAction action, String pos, String altPos) {
        this.prompt = prompt;
        this.action = action;
        this.pos = pos;
        this.altPos = altPos;
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

    public String getAltPos() {
        return altPos;
    }
}

