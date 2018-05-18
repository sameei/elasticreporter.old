package test

trait Toy

trait Child {
    type T <: Toy
    def toys : Seq[T]
    def play(toys: Seq[T]): Unit
}

trait Parent { parent =>
    type C <: Child
    def firstChild: C
}

trait Home {
    def parent: Parent
    def toys: Seq[Parent#C#T]
    def apply() = {
        val ts = toys
        parent.firstChild.play(toys)
    }
}


object X {

    trait Toy

    trait Child[T <: Toy] {
        def toys : Seq[T]
        def play(toys: Seq[T]): Unit
    }

    trait Parent[T <: Toy, C <: Child[T]] {
        def firstChild: C
    }

    trait Home[T <: Toy, C <: Child[T], P <: Parent[T, C]] {
        def parent: P
        def toys: Seq[T]
        def apply() = {
            val ts = toys
            parent.firstChild.play(toys)
        }
    }


}