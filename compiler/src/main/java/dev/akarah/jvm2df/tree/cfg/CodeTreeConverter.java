package dev.akarah.jvm2df.tree.cfg;

import dev.akarah.jvm2df.tree.CompilationGraph;
import dev.akarah.jvm2df.tree.instructions.*;
import dev.akarah.jvm2df.tree.instructions.Terminator;

import java.lang.classfile.CodeElement;
import java.lang.classfile.Label;
import java.lang.classfile.Opcode;
import java.lang.classfile.TypeKind;
import java.lang.classfile.instruction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Converts a series of JVM instructons in a Basic Block into
 * a {@link CodeTree}.
 */
public class CodeTreeConverter {
    List<CodeTree> statements;
    List<CodeTree> stack;
    Function<Label, Integer> labelConverter;
    CompilationGraph graph;


    public CodeTreeConverter(List<CodeTree> statements, Function<Label, Integer> labelConverter, CompilationGraph graph) {
        this.statements = statements;
        this.stack = new ArrayList<>();
        this.labelConverter = labelConverter;
        this.graph = graph;
    }

    public void convert(CodeElement codeElement, int offset) {
        switch (codeElement) {
            case ConstantInstruction constantInstruction -> {
                this.stack.add(new CodeTree.Constant(constantInstruction.constantValue()));
            }
            case ReturnInstruction instruction -> {
                if (instruction.typeKind().equals(TypeKind.VOID)) {
                    this.statements.add(new Terminator.ReturnVoid());
                } else {
                    this.statements.add(new Terminator.ReturnValue(this.stack.removeLast()));
                }
            }
            case IncrementInstruction instruction -> {
                this.statements.add(new CodeTree.IncrementLocal(instruction.slot(), new CodeTree.Constant(instruction.constant())));
            }
            case StoreInstruction instruction -> {
                this.statements.add(new CodeTree.StoreLocal(instruction.slot(), this.stack.removeLast()));
            }
            case LoadInstruction instruction -> {
                this.stack.add(new CodeTree.LoadLocal(instruction.slot()));
            }
            case NewPrimitiveArrayInstruction _, NewReferenceArrayInstruction _ -> {
                this.stack.add(new CodeTree.ArrayNew(this.stack.removeLast()));
            }
            case ArrayStoreInstruction _ -> {
                var value = this.stack.removeLast();
                var index = this.stack.removeLast();
                var array = this.stack.removeLast();
                this.statements.add(new CodeTree.ArrayStore(array, index, value));
            }
            case ArrayLoadInstruction _ -> {
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
                        var value = this.stack.removeLast();
                        this.statements.add(new CodeTree.StoreLocal(num, value));
                        this.stack.add(new CodeTree.LoadLocal(num));
                        this.stack.add(new CodeTree.LoadLocal(num));
                    }
                    case DUP2 -> {
                        var value = this.stack.removeLast();
                        this.statements.add(new CodeTree.StoreLocal(num, value));
                        this.stack.add(new CodeTree.LoadLocal(num));
                        this.stack.add(new CodeTree.LoadLocal(num));
                        this.stack.add(new CodeTree.LoadLocal(num));
                    }
                    default -> throw new RuntimeException("i'm doing this later this SUCKS");
                }
            }
            case InvokeInstruction instruction -> this.invoke(instruction, offset);
            case BranchInstruction instruction -> this.branch(instruction, offset);
            case OperatorInstruction instruction -> this.operator(instruction, offset);
            case NewObjectInstruction instruction -> this.newObj(instruction);
            case FieldInstruction instruction -> this.field(instruction);
            case TypeCheckInstruction _ -> {
                // TODO: typecheck :3
            }
            default -> {
                this.stack.add(new CodeTree.Unknown(codeElement));
            }
        }
    }

    private void newObj(NewObjectInstruction instruction) {
        this.statements.add(new CodeTree.StoreLocal(
                Integer.MAX_VALUE - 1,
                new CodeTree.ObjectNew(instruction.className().asInternalName())
        ));
        this.stack.add(new CodeTree.LoadLocal(Integer.MAX_VALUE - 1));
    }

    private void field(FieldInstruction instruction) {
        switch (instruction.opcode()) {
            case GETSTATIC -> this.stack.add(new CodeTree.ObjectGetStatic(
                    instruction.owner().asInternalName(),
                    instruction.field().name().stringValue()
            ));
            case PUTSTATIC -> this.statements.add(new CodeTree.ObjectSetStatic(
                    instruction.owner().asInternalName(),
                    instruction.field().name().stringValue(),
                    this.stack.removeLast()
            ));
            case GETFIELD -> this.stack.add(new CodeTree.ObjectGetField(
                    this.stack.removeLast(),
                    instruction.field().name().stringValue()
            ));
            case PUTFIELD -> {
                var value = this.stack.removeLast();
                var obj = this.stack.removeLast();
                this.statements.add(new CodeTree.ObjectSetField(
                        obj,
                        instruction.field().name().stringValue(),
                        value
                ));
            }
            default -> {
            }
        }
    }

    private void operator(OperatorInstruction instruction, int offset) {
        switch (instruction.opcode()) {
            case DADD, FADD, IADD, LADD -> {
                this.stack.add(new CodeTree.BinOp(BinOpType.ADD, stack.removeLast(), stack.removeLast()));
            }
            case DSUB, FSUB, ISUB, LSUB -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.BinOp(BinOpType.SUB, lhs, rhs));
            }
            case DMUL, FMUL, IMUL, LMUL -> {
                this.stack.add(new CodeTree.BinOp(BinOpType.MUL, stack.removeLast(), stack.removeLast()));
            }
            case DDIV, FDIV, IDIV, LDIV -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.BinOp(BinOpType.DIV, lhs, rhs));
            }
            case DREM, FREM, IREM, LREM -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.BinOp(BinOpType.MOD, lhs, rhs));
            }
            case DNEG, FNEG, INEG, LNEG -> {
                this.stack.add(new CodeTree.Negate(stack.removeLast()));
            }
            case ISHL, LSHL -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.BinOp(BinOpType.SHL, lhs, rhs));
            }
            case ISHR, LSHR -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.BinOp(BinOpType.SHR, lhs, rhs));
            }
            case IAND, LAND ->
                    this.stack.add(new CodeTree.BinOp(BinOpType.AND, stack.removeLast(), stack.removeLast()));
            case IXOR, LXOR ->
                    this.stack.add(new CodeTree.BinOp(BinOpType.XOR, stack.removeLast(), stack.removeLast()));
            case IOR, LOR -> this.stack.add(new CodeTree.BinOp(BinOpType.OR, stack.removeLast(), stack.removeLast()));
            case DCMPG, DCMPL, FCMPG, FCMPL -> {
                var rhs = stack.removeLast();
                var lhs = stack.removeLast();
                this.stack.add(new CodeTree.BinOp(BinOpType.COMPARE_DOUBLES, lhs, rhs));
            }
            case ARRAYLENGTH -> {
                this.stack.add(new CodeTree.ArrayLength(this.stack.removeLast()));
            }
            default -> {
            }
        }
    }

    private void invoke(InvokeInstruction instruction, int offset) {
        List<CodeTree> params = new ArrayList<>();
        for (var _ : instruction.typeSymbol().parameterList()) {
            params.add(this.stack.removeLast());
        }
        if (instruction.opcode() != Opcode.INVOKESTATIC) {
            params.add(this.stack.removeLast());
        }
        params = params.reversed();
        var invoke = new CodeTree.Invoke(
                instruction.method().owner(),
                new CompilationGraph.MethodOutline(
                        instruction.method().name().stringValue(),
                        instruction.typeSymbol()
                ),
                params,
                switch (instruction.opcode()) {
                    case INVOKESTATIC -> InvokeStyle.STATIC;
                    case INVOKESPECIAL -> InvokeStyle.VIRTUAL_EXACT;
                    case INVOKEVIRTUAL, INVOKEINTERFACE -> InvokeStyle.VIRTUAL_OVERRIDABLE;
                    default -> InvokeStyle.DYNAMIC_CALL_SITE;
                }
        );
        if (TypeKind.from(instruction.typeSymbol().returnType()) == TypeKind.VOID) {
            this.statements.add(invoke);
        } else {
            this.stack.add(invoke);
        }
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
        if (ZERO_ADDING_BRANCH_OPCODES.contains(instruction.opcode())) {
            this.stack.add(new CodeTree.Constant(0));
        }
        if (SWAPPED_BRANCH_OPCODES.contains(instruction.opcode())) {
            var rhs = stack.removeLast();
            var lhs = stack.removeLast();
            stack.add(rhs);
            stack.add(lhs);
        }

        switch (instruction.opcode()) {
            case GOTO, GOTO_W -> statements.add(new Terminator.Jump(this.labelToOffset(instruction.target())));
            case IFEQ, IF_ACMPEQ, IF_ICMPEQ, IFNULL -> stack.add(new CodeTree.Compare(
                    ComparisonType.EQUAL, this.stack.removeLast(), this.stack.removeLast()));
            case IFNE, IF_ACMPNE, IF_ICMPNE, IFNONNULL -> stack.add(new CodeTree.Compare(
                    ComparisonType.NOT_EQUAL, this.stack.removeLast(), this.stack.removeLast()));
            case IFGE, IF_ICMPGE -> stack.add(new CodeTree.Compare(
                    ComparisonType.GREATER_THAN_OR_EQ, this.stack.removeLast(), this.stack.removeLast()));
            case IFGT, IF_ICMPGT -> stack.add(new CodeTree.Compare(
                    ComparisonType.GREATER_THAN, this.stack.removeLast(), this.stack.removeLast()));
            case IFLE, IF_ICMPLE -> stack.add(new CodeTree.Compare(
                    ComparisonType.LESS_THAN_OR_EQ, this.stack.removeLast(), this.stack.removeLast()));
            case IFLT, IF_ICMPLT -> stack.add(new CodeTree.Compare(
                    ComparisonType.LESS_THAN, this.stack.removeLast(), this.stack.removeLast()));
            default -> {
            }
        }
        if (instruction.opcode().toString().contains("IF")) {
            this.statements.add(
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
