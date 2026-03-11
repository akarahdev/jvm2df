package dev.akarah.jvm2df.bytecode;

import java.lang.classfile.CodeModel;
import java.lang.classfile.Label;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.BranchInstruction;
import java.lang.classfile.instruction.LabelTarget;
import java.util.*;

public class MethodFlowAnalysis {
    CodeModel codeModel;
    Label beginningLabel;
    Map<Label, List<Label>> successors = new HashMap<>();
    Map<Label, List<Label>> predecessors = new HashMap<>();
    List<Label> recursiveLabels = new ArrayList<>();

    public MethodFlowAnalysis(CodeModel codeModel) {
        this.codeModel = codeModel;
    }

    public MethodFlowAnalysis analyze() {
        this.findSuccessors();
        this.findPredecessors();
        this.findRecursiveLabels();
        return this;
    }

    private void findSuccessors() {
        Label currentLabel = null;
        for(var codeElement : codeModel.elementList()) {
            if(codeElement instanceof Label label) {
                if(this.beginningLabel == null) {
                    this.beginningLabel = label;
                }
                if(currentLabel != null) {
                    var list = this.successors.get(currentLabel);
                    if(!list.contains(label)) {
                        list.add(label);
                    }
                }
                currentLabel = label;
                this.successors.put(currentLabel, new ArrayList<>());
            }
            if(codeElement instanceof BranchInstruction branchInstruction) {
                var list = this.successors.get(currentLabel);
                if(!list.contains(branchInstruction.target())) {
                    list.add(branchInstruction.target());
                }
            }
        }
    }

    private void findPredecessors() {
        for(var label : this.successors.keySet()) {
            var predecessors = new ArrayList<Label>();
            for(var possiblePredecessor : this.successors.keySet()) {
                if(this.successors.get(possiblePredecessor).contains(label)) {
                    predecessors.add(possiblePredecessor);
                }
            }
            this.predecessors.put(label, predecessors);
        }
    }

    private void findRecursiveLabels() {
        for (Label label : successors.keySet()) {
            if (isRecursive(label)) {
                recursiveLabels.add(label);
            }
        }
    }

    private boolean isRecursive(Label target) {
        Set<Label> visited = new HashSet<>();
        List<Label> initialSuccessors = successors.get(target);
        if (initialSuccessors == null) return false;
        Queue<Label> queue = new LinkedList<>(initialSuccessors);

        while (!queue.isEmpty()) {
            Label current = queue.poll();
            if (current.equals(target)) {
                return true;
            }
            if (visited.add(current)) {
                List<Label> nextLabels = successors.get(current);
                if (nextLabels != null) {
                    queue.addAll(nextLabels);
                }
            }
        }
        return false;
    }

    public String toString() {
        var sb = new StringBuilder();
        sb.append("ControlFlowAnalysis{\n");
        if(this.beginningLabel != null) {
            sb.append(" begin=");
            appendLabelBci(sb, this.beginningLabel);
            sb.append("\n");
        }
        sb.append(" successors=\n");
        for(var successor : this.successors.entrySet()) {
            listLabelMapping(sb, successor);
        }
        sb.append(" predecessors=\n");
        for(var predecessor : this.predecessors.entrySet()) {
            listLabelMapping(sb, predecessor);
        }
        sb.append(" recursiveLabels=");
        listLabelArray(sb, this.recursiveLabels);
        sb.append("\n}");
        return sb.toString();
    }

    private void listLabelMapping(StringBuilder sb, Map.Entry<Label, List<Label>> predecessor) {
        sb.append("  ");
        appendLabelBci(sb, predecessor.getKey());
        sb.append("=");
        this.listLabelArray(sb, predecessor.getValue());
    }

    private void listLabelArray(StringBuilder sb, List<Label> predecessor) {
        sb.append("[");
        for(var label : predecessor) {
            appendLabelBci(sb, label);
            sb.append(",");
        }
        sb.append("]\n");
    }

    private void appendLabelBci(StringBuilder sb, Label label) {
        var bci = ((CodeAttribute) this.codeModel).labelToBci(label);
        sb.append("@").append(bci);
    }
}
