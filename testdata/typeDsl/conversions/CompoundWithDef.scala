trait A
trait B
val g = new A with B {
  def a = 1
}

val g: {
  type T >: Seq[List[String]] <: B

}