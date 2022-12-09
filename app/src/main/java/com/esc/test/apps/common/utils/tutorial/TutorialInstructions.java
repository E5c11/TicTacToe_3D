package com.esc.test.apps.common.utils.tutorial;

import com.esc.test.apps.App;
import com.esc.test.apps.R;
import com.esc.test.apps.data.objects.entities.PlayerInstruction;
import com.esc.test.apps.data.objects.entities.PcInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialInstructions {

    public static final List<PlayerInstruction> user = new ArrayList<>() {{

        // First round
        add(new PlayerInstruction(App.res.getString(R.string.first_instruction), TutAction.FLASH, "0"));
        add(new PlayerInstruction(App.res.getString(R.string.third_instruction), TutAction.FLASH, "16"));
        add(new PlayerInstruction(App.res.getString(R.string.fourth_instruction), TutAction.WAIT, "32"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "32"));
        add(new PlayerInstruction(App.res.getString(R.string.sixth_instruction), TutAction.WAIT, "48"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "48"));
        add(new PlayerInstruction(App.res.getString(R.string.eighth_instruction), TutAction.RESTART, "48"));

        // Second round
        add(new PlayerInstruction(App.res.getString(R.string.tenth_instruction), TutAction.FLASH, "0"));
        add(new PlayerInstruction(App.res.getString(R.string.eleventh_instruction), TutAction.WAIT, "17", "20"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "17", "20"));
        add(new PlayerInstruction(App.res.getString(R.string.twelfth_instruction), TutAction.WAIT, "34", "40"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "34", "40"));
        add(new PlayerInstruction(App.res.getString(R.string.thirteenth_instruction), TutAction.WAIT, "51", "60"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "51", "60"));
        add(new PlayerInstruction(App.res.getString(R.string.fourteenth_instruction), TutAction.RESTART, "51", "60"));

        // Third round
        add(new PlayerInstruction(App.res.getString(R.string.tenth_instruction), TutAction.FLASH, "0"));
        add(new PlayerInstruction(App.res.getString(R.string.fifteenth_instruction), TutAction.WAIT, "21"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "21"));
        add(new PlayerInstruction(App.res.getString(R.string.sixteenth_instruction), TutAction.WAIT, "42"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "42"));
        add(new PlayerInstruction(App.res.getString(R.string.seventeenth_instruction), TutAction.WAIT, "63"));
        add(new PlayerInstruction(App.res.getString(R.string.confirm_prompt), TutAction.WAIT, "63"));
        add(new PlayerInstruction(App.res.getString(R.string.eighteenth_instruction), TutAction.END, "63"));

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

        //Third round
        add(new PcInstruction("3"));
        add(new PcInstruction("22"));
        add(new PcInstruction("41"));
    }};

    public static final List<List<String>> winningRow = new ArrayList<>() {{
        add(Arrays.asList("0", "16", "32", "48"));
        add(Arrays.asList("0", "17", "34", "51"));
        add(Arrays.asList("0", "20", "40", "60"));
        add(Arrays.asList("0", "21", "42", "63"));
    }};

}
