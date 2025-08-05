package com.haitao.ai.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "think finished, not need act";
            }
            return act();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "step failed,exception is : " + ex.getMessage();
        }
    }

    public abstract boolean think();

    public abstract String act();
}
