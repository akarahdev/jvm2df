import diamondfire.Control;

public class Cat extends Animal {
    @Override
    public void makeSound() {
        Control.debug("meow");
    }

    @Override
    public String toString() {
        return "Cat";
    }
}
