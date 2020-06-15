public class AsmTest implements IAsmTest {
	private final AsmTest finalField = new AsmTest();
	public int unfinalField;

	@Override public int primitiveMethod() {
		return this.unfinalField;
	}

	@Override public AsmTest normalMethod() {
		return this.finalField;
	}

	@Override public AsmTest normalMethodWithParam(int i) {
		this.unfinalField = i;
		return this;
	}
}
