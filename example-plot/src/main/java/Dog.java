import diamondfire.Control;

public class Dog extends Animal {
    @Override
    public void makeSound() {
        Control.debug("bark");
    }

    @Override
    public String toString() {
        return "Dog";
    }
}
