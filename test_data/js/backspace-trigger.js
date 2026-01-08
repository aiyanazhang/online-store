class DataProcessor {
	constructor() {
		this.result = null;
	}

	processData(input) {
		const tempValue = input * 2;
		const finalResult = tempValue + 10;
		return finalResult;
	}

	validateInput(data) {
		const isValid = data !== null;
		return isValid;
	}
}
