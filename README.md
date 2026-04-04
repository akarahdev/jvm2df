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
- [ ] `df.value.*` APIs
    - [ ] `df.value.Location`
    - [ ] `df.value.Text`
    - [ ] `df.value.Vector`
    - [ ] `df.value.Sound`
    - [ ] `df.value.Particle`
    - [ ] `df.value.Potion`
- [x] `df.value.bucket.*`
    - [x] `df.value.bucket.BucketHandle`
    - [x] `df.value.bucket.BucketVariableKey`
- [ ] `df.value.selection.*` APIs
    - [ ] `df.value.selection.AbstractSelection`
    - [ ] `df.value.selection.PlayerSelection`
    - [ ] `df.value.selection.EntitySelection`
- [x] Enhanced `for` loop
- [x] `java.lang.Thread`