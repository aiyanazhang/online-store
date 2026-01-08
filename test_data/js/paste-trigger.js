class EventHandler {
	constructor() {
		this.listeners = [];
	}

	init() {
		// PASTE_SINGLE_LINE_MARKER
	}

	setup() {
		// PASTE_MULTI_LINE_MARKER
		const handler = this.createHandler(
			() => {
				this.listeners.forEach(listener => listener());
			}
		);
	}
}
