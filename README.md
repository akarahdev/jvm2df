# JVM2DF

JVM2DF is a Java bytecode transpiler to DiamondFire codeblocks.

Supported features:

- Primitives & their operations
- Local variables, for loops, while loops, if conditions
- Arrays
- Objects
- Garbage collection

Note that compilation currently only works on Node Beta due to bucket variables being a dependency of JVM2DF.

Roadmap:

- [x] `java.lang.String` APIs
- [ ] Control flow optimization
    - [x] Simple dominator tree algorithms
    - [ ] Relooper
- [ ] Polymorphism
    - [x] `extends` clauses and virtual dispatch
    - [ ] `abstract class`
    - [x] `interface` and `invokeinterface`
- [ ] `invokedynamic`
    - [ ] `java.lang.MethodHandle` and related APIs
    - [ ] String concat
    - [ ] Lambdas
    - [ ] Enum switch
    - [ ] Pattern matching
- [ ] `diamondfire.value.*` APIs
    - [ ] `diamondfire.value.Location`
    - [ ] `diamondfire.value.Text`
    - [ ] `diamondfire.value.Vector`
    - [ ] `diamondfire.value.Sound`
    - [ ] `diamondfire.value.Particle`
    - [ ] `diamondfire.value.Potion`
- [x] `diamondfire.value.bucket.*`
    - [x] `diamondfire.value.bucket.BucketHandle`
    - [x] `diamondfire.value.bucket.BucketVariableKey`
- [ ] `diamondfire.value.selection.*` APIs
    - [ ] `diamondfire.value.selection.AbstractSelection`
    - [ ] `diamondfire.value.selection.PlayerSelection`
    - [ ] `diamondfire.value.selection.EntitySelection`
- [x] Enhanced `for` loop
- [x] `java.lang.Thread`