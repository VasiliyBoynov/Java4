package SecureChatClient.Command;

import java.util.List;

public abstract class Instruction {
public Command Instruction;

    public Instruction(Command instruction) {
        Instruction = instruction;
    }

    public String getInstruction() {
        return Instruction.toString();
    }

    public abstract List<String> toJSON();

    public abstract byte[] objToByte();


}
