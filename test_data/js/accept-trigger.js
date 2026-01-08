class BufferManager {
	constructor() {
		this.BufferReader = null;
		this.FileWriter = null;
	}

	createReader() {
		const reader = new BufferReader();
		return reader;
	}

	writeToFile(data) {
		const writer = new FileWriter();
		writer.write(data);
	}

	processBufferData(buffer) {
		const processor = buffer.dataProcessor;
		return processor;
	}

	testWrite(data){
		this.writeToFile(data);
		this.createReader().setDataProcessor(data);
	}

	testRead(data){
		this.writeToFile(data);
		this.createReader().setDataProcessor(data);
	}
}	

class BufferReader {
	constructor() {
		this.dataProcessor = null;
	}

	setDataProcessor(processor) {
		this.dataProcessor = processor;
	}
}
