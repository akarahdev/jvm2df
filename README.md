# JVM2DF

JVM2DF is a Java bytecode transpiler to DiamondFire codeblocks.

Supported features:

- Primitives & their operations
- Local variables, for loops, while loops, if conditions
- Arrays
- Objects

Roadmap:

- [x] `java.lang.String` APIs
- [ ] Control flow optimization
    - [x] Simple dominator tree algorithms
    - [ ] Relooper
- [ ] Polymorphism
    - [x] `extends` clauses and virtual dispatch
    - [ ] `abstract class`
    - [ ] `interface` and `invokeinterface`
- [ ] `java.lang.MethodHandle` and related APIs
- [ ] `invokedynamic`
- [ ] `diamondfire.value.*` APIs
- [ ] Enhanced `for` loop