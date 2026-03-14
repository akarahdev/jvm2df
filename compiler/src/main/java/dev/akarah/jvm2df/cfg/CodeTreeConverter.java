package dev.akarah.jvm2df.cfg;

import java.lang.classfile.*;
import java.lang.classfile.instruction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class CodeTreeConverter {
    List<CodeTree> stack;
    Function<Label, Integer> labelConverter;


    public CodeTreeConverter(List<CodeTree> stack, Function<Label, Integer> labelConverter) {
        this.stack = stack;
        this.labelConverter = labelConverter;
    }

    public void convert(CodeElement codeElement, int offset) {
        switch (codeElement) {
            case ConstantInstruction constantInstruction -> {
                this.stack.add(new CodeTree.Constant(constantInstruction.constantValue()));
            }
            case ReturnInstruction instruction -> {
                if(instruction.typeKind().equals(TypeKind.VOID)) {
                    this.stack.add(new Terminator.ReturnVoid());
                } else {
                    this.stack.add(new Terminator.ReturnValue(this.stack.removeLast()));
                }
            }
            case IncrementInstruction instruction -> {
                this.stack.add(new CodeTree.IncrementLocal(instruction.slot(), new CodeTree.Constant(instruction.constant())));
            }
            case StoreInstruction instruction -> {
                this.stack.add(new CodeTree.StoreLocal(instruction.slot(), this.stack.removeLast()));
            }
            case LoadInstruction instruction -> {
                this.stack.add(new CodeTree.LoadLocal(instruction.slot()));
            }
            case NewPrimitiveArrayInstruction instruction -> {
                this.stack.add(new CodeTree.ArrayNew(this.stack.removeLast()));
            }
            case NewReferenceArrayInstruction instruction -> {
                this.stack.add(new CodeTree.ArrayNew(this.stack.removeLast()));
            }
            case ArrayStoreInstruction instruction -> {
                var value = this.stack.removeLast();
                var index = this.stack.removeLast();
                var array = this.stack.removeLast();
                this.stack.add(new CodeTree.ArrayStore(array, index, value));
            }
            case ArrayLoadInstruction instruction -> {
                var index = this.stack.removeLast();
                var array = this.stack.removeLast();
                this.stack.add(new CodeTree.ArrayIndex(array, index));
            }
            case StackInstruction instruction -> {
                var num = 1000000 - 1 + new Random().nextInt(Integer.MAX_VALUE - 1000000);
                switch (instruction.opcode()) {
                    case POP -> {
                        this.stack.removeLast();
                    }
                    case POP2 -> {
                        this.stack.removeLast();
                        this.stack.removeLast();
                    }
                    case DUP -> {
                        this.stack.add(new CodeTree.StoreLocal(num, this.stack.removeLast()));
                        this.stack.add(new CodeTree.LoadLocal(num));
                    }
                    case DUP2 -> {
                        this.stack.add(new CodeTree.StoreLocal(num, this.stack.removeLast()));
                        this.stack.add(new CodeTree.LoadLocal(num));
                        this.stack.add(new CodeTree.LoadLocal(num));
                    }
                    default -> throw new RuntimeException("i'm doing this later this SUCKS");
                }
            }
            case InvokeInstruction instruction -> this.invoke(instruction, offset);
            case BranchInstruction instruction -> this.branch(instruction, offset);
            case OperatorInstruction instruction -> this.operator(instruction, offset);
            default -> {
                this.stack.add(new CodeTree.Unknown(codeElement));
            }
        };
    }

    private void operator(OperatorInstruction instruction, int offset) {
        switch (instruction.opcode()) {
            case DADD, FADD, IADD, LADD -> {
                this.stack.add(new CodeTree.Add(stack.removeLast(), stack.removeLast()));
            }
            case DSUB, FSUB, ISUB, LSUB -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.Sub(lhs, rhs));
            }
            case DMUL, FMUL, IMUL, LMUL -> {
                this.stack.add(new CodeTree.Mul(stack.removeLast(), stack.removeLast()));
            }
            case DDIV, FDIV, IDIV, LDIV -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.Div(lhs, rhs));
            }
            case DREM, FREM, IREM, LREM -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.Mod(lhs, rhs));
            }
            case DNEG, FNEG, INEG, LNEG -> {
                this.stack.add(new CodeTree.Negate(stack.removeLast()));
            }
            case ISHL, LSHL -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.ShiftLeft(lhs, rhs));
            }
            case ISHR, LSHR -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.ShiftRight(lhs, rhs));
            }
            case IAND, LAND -> this.stack.add(new CodeTree.And(stack.removeLast(), stack.removeLast()));
            case IXOR, LXOR -> this.stack.add(new CodeTree.Xor(stack.removeLast(), stack.removeLast()));
            case IOR, LOR -> this.stack.add(new CodeTree.Or(stack.removeLast(), stack.removeLast()));
            case DCMPG, DCMPL, FCMPG, FCMPL -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.CompareNumbers(lhs, rhs));
            }
            case ARRAYLENGTH -> {
                this.stack.add(new CodeTree.ArrayLength(this.stack.removeLast()));
            }
        }
    }

    private void invoke(InvokeInstruction instruction, int offset) {
        List<CodeTree> params = new ArrayList<>();
        for(var parameter : instruction.typeSymbol().parameterList()) {
            params.add(this.stack.removeLast());
        }
        if(instruction.opcode() != Opcode.INVOKESTATIC) {
            params.add(this.stack.removeLast());
        }
        params = params.reversed();
        stack.add(new CodeTree.Invoke(
                instruction.owner().asInternalName()
                    + "#"
                    + instruction.name()
                    + instruction.typeSymbol().descriptorString(),
                params
        ));
    }


    private final Set<Opcode> ZERO_ADDING_BRANCH_OPCODES = Set.of(
            Opcode.IFEQ, Opcode.IFNULL, Opcode.IFNE, Opcode.IFNONNULL,
            Opcode.IFGE, Opcode.IFLE, Opcode.IFGT, Opcode.IFLT
    );

    private final Set<Opcode> SWAPPED_BRANCH_OPCODES = Set.of(
            Opcode.IFGE, Opcode.IF_ICMPGE,
            Opcode.IFLE, Opcode.IF_ICMPLE,
            Opcode.IFGT, Opcode.IF_ICMPGT,
            Opcode.IFLT, Opcode.IF_ICMPLT
    );

    private void branch(BranchInstruction instruction, int offset) {
        if(ZERO_ADDING_BRANCH_OPCODES.contains(instruction.opcode())) {
            this.stack.add(new CodeTree.Constant(0));
        }
        if(SWAPPED_BRANCH_OPCODES.contains(instruction.opcode())) {
            var rhs = stack.removeLast();
            var lhs = stack.removeLast();
            stack.add(rhs);
            stack.add(lhs);
        }

        switch (instruction.opcode()) {
            case GOTO, GOTO_W -> stack.add(new Terminator.Jump(this.labelToOffset(instruction.target())));
            case IFEQ, IF_ACMPEQ, IF_ICMPEQ, IFNULL -> stack.add(new CodeTree.IsEqual(this.stack.removeLast(), this.stack.removeLast()));
            case IFNE, IF_ACMPNE, IF_ICMPNE, IFNONNULL -> stack.add(new CodeTree.IsNotEqual(this.stack.removeLast(), this.stack.removeLast()));
            case IFGE, IF_ICMPGE -> stack.add(new CodeTree.IsGE(this.stack.removeLast(), this.stack.removeLast()));
            case IFGT, IF_ICMPGT -> stack.add(new CodeTree.IsGT(this.stack.removeLast(), this.stack.removeLast()));
            case IFLE, IF_ICMPLE -> stack.add(new CodeTree.IsLE(this.stack.removeLast(), this.stack.removeLast()));
            case IFLT, IF_ICMPLT -> stack.add(new CodeTree.IsLT(this.stack.removeLast(), this.stack.removeLast()));
        }
        if(instruction.opcode().toString().contains("IF")) {
            this.stack.add(
                    new Terminator.BranchIf(
                            stack.removeLast(),
                            this.labelToOffset(instruction.target()),
                            offset + instruction.sizeInBytes()
                    )
            );
        }
    }

    private int labelToOffset(Label label) {
        return this.labelConverter.apply(label);
    }


}
