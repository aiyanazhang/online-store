// THIS_IS_TOOL_TEST_FIXTURE - 工具调用测试桩文件，请勿删除
// 用途：为自动化测试提供确定性的搜索目标

export class ToolTestFixtureClass {
	public fixtureMethod(): string {
		return 'tool-test-fixture-content';
	}
}

// 故意的语法错误（供 get_problems 检测）
const brokenSyntax: number = "this is not a number";  // Type error
