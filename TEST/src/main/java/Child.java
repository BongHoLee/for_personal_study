public class Child extends Parent{
    public int a = 1000;

    public static void main(String[] args) {
        Parent child = new Child();
        System.out.println(child.a);
    }

}
