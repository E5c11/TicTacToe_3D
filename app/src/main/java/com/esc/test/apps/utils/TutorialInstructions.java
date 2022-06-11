package com.esc.test.apps.utils;

import static com.esc.test.apps.utils.TutAction.FLASH;
import static com.esc.test.apps.utils.TutAction.HIGHLIGHT;
import static com.esc.test.apps.utils.TutAction.RESTART;
import static com.esc.test.apps.utils.TutAction.WAIT;

import com.esc.test.apps.App;
import com.esc.test.apps.R;
import com.esc.test.apps.entities.PlayerInstruction;
import com.esc.test.apps.entities.PcInstruction;

import java.util.ArrayList;
import java.util.List;

public class TutorialInstructions {

    public static final List<PlayerInstruction> user = new ArrayList<>() {{

        // First round
        add(new PlayerInstruction(App.res.getString(R.string.first_instruction), FLASH, "0"));
//        add(new PlayerInstruction(App.res.getString(R.string.second_instruction), HIGHLIGHT, "0"));
        add(new PlayerInstruction(App.res.getString(R.string.third_instruction), FLASH, "16"));
        add(new PlayerInstruction(App.res.getString(R.string.fourth_instruction), WAIT, "32"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), WAIT, "32"));
        add(new PlayerInstruction(App.res.getString(R.string.sixth_instruction), WAIT, "48"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), WAIT, "48"));
        add(new PlayerInstruction(App.res.getString(R.string.eighth_instruction), RESTART, "48"));

//        // Second round
        add(new PlayerInstruction(App.res.getString(R.string.tenth_instruction), FLASH, "0"));
        add(new PlayerInstruction(App.res.getString(R.string.eleventh_instruction), WAIT, "17", "20"));
        add(new PlayerInstruction(App.res.getString(R.string.twelfth_instruction), WAIT, "34", "40"));
        add(new PlayerInstruction(App.res.getString(R.string.thirteenth_instruction), WAIT, "51", "60"));
//        add(new Instruction(App.res.getString(R.string.)));

    }};

    public static final List<PcInstruction> pc = new ArrayList<>() {{

        // First round
        add(new PcInstruction("9"));
        add(new PcInstruction("6"));
        add(new PcInstruction("3"));

        //Second round
        add(new PcInstruction("12"));
        add(new PcInstruction("25"));
        add(new PcInstruction("38"));
    }};

}
