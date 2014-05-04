case class Bird(val name: String) extends Object {
  def fly(height: Int): Unit = {}
}


val b = new Bird("Polly the parrot") {val callsign = name}

