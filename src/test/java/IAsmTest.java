public interface IAsmTest {
	// primitive methods don't even need impl
	int primitiveMethod();

	default IAsmTest normalMethod() {
		return ((AsmTest)this).normalMethod();
	}

	default IAsmTest normalMethodWithParam(int i) {
		return ((AsmTest)this).normalMethodWithParam(i);
	}

	default void setter(int i) {
		((AsmTest)this).unfinalField = i;
	}
}
