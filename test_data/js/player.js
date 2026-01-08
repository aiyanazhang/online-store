class Player {
	constructor(gameEngine) {
		// 引用游戏主引擎实例，用于访问游戏状态和其他系统
		this.gameEngine = gameEngine;

		// 玩家初始世界坐标位置（像素单位）
		this.x = 400; // 初始位置X
		this.y = 300; // 初始位置Y

		this.size = 12;
		this.speed = 2;
		this.direction = 'right';
		this.nextDirection = null;
		this.animationFrame = 0;
		this.mouthOpen = true;
		this.lastMoveTime = 0;
		this.reset();
	}

	reset() {
		if (this.gameEngine.maze) {
			const centerRow = Math.floor(this.gameEngine.maze.rows / 2);
			const centerCol = Math.floor(this.gameEngine.maze.cols / 2);
			const centerPos = this.gameEngine.maze.getWorldPosition(centerCol, centerRow);
			this.x = centerPos.x;
			this.y = centerPos.y;
		} else {
			this.x = 400;
			this.y = 300;
		}

		this.direction = 'right';
		this.nextDirection = null;
		this.animationFrame = 0;
		this.mouthOpen = true;
	}
}
