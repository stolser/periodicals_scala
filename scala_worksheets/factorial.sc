import scala.annotation.tailrec

def factorial(x: Int): Int = {
	require(x >= 0)
	@tailrec
	def factAcc(acc: Int, y: Int): Int = y match {
		case 0 | 1 => acc
		case _ => factAcc(acc * y, y - 1)
	}

	factAcc(acc = 1, x)
}

for(i <- 0 to 5)
	println(s"$i! = ${factorial(i)}")