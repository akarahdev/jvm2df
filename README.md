# JVM2DF
JVM2DF is a Java bytecode transpiler to DiamondFire codeblocks.

Supported features:
- Primitives & their operations
- Local variables, for loops, while loops, if conditions
- Arrays
- Objects

Roadmap:
- [ ] `java.lang.String` APIs
- [ ] Control flow optimization
  - [ ] Simple dominator tree algorithms
  - [ ] Relooper
- [ ] Polymorphism
  - [ ] `extends` clauses and virtual dispatch
    - There is a form of virtual dispatch supported, but it is only 
    direct type dispatch. Method overrides do not work properly yet.
  - [ ] `abstract class`
  - [ ] `interface` and `invokeinterface`
- [ ] `java.lang.MethodHandle` and related APIs
- [ ] `invokedynamic`
- [ ] `diamondfire.List` APIs
- [ ] `diamondfire.Selection` APIs
- [ ] `diamondfire.Location` APIs
- [ ] `diamondfire.Vector` APIs
- [ ] Enhanced `for` loop