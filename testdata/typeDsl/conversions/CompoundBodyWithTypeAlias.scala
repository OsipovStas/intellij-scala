trait Super

val b: Super {type T = Int} = new Super  {
  type T = Int
  val callsign = Seq(432)
}