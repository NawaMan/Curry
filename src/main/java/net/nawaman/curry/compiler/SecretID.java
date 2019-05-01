package net.nawaman.curry.compiler;

import java.util.Random;

final public class SecretID {
	static final Random R = new Random();
	int ID = R.nextInt() + R.nextInt();
}
