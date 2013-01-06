<!DOCTYPE html>
<html>
<head>
<%@ include file="../include/ext/javaScriptInclude.jsp"%>
<%@ include file="../include/WebDashboardInclude.jsp"%>
<%@ include file="../include/halookInclude.jsp"%>
</head>
<body id="main" oncontextmenu="return false;" onload="self.focus();">
	<div id="persArea"></div>
	<input id="treeData" type="hidden" value='${treeData}' />
	<script type="text/javascript">
		var viewArea1 = {};
		var viewArea2 = {};

		viewArea1.width = 300;
		viewArea1.height = 800;
		viewArea1.rowspan = 1;
		viewArea1.colspan = 1;

		viewArea2.width = 900;
		viewArea2.height = 800;
		viewArea2.rowspan = 1;
		viewArea2.colspan = 1;

		var table = [ [ new wgp.PerspactiveModel(viewArea1),
				new wgp.PerspactiveModel(viewArea2) ] ];
		var perspactiveView = new wgp.PerspactiveView({
			id : "persArea",
			collection : table
		});
		perspactiveView.dropView("persArea_drop_0_0", "tree_area");
		perspactiveView.dropView("persArea_drop_0_1", "contents_area");

		var appView = new wgp.AppView();
	</script>

	<script src="<%=request.getContextPath()%>/resources/plugins/halook/js/common/user.js"
		type="text/javaScript"></script>

	<script>
		var treeView = new wgp.TreeView({
			id : "tree_area",
			targetId : "contents_area"
		});
		appView.addView(treeView, wgp.constants.TREE.DATA_ID);
		websocketClient = new wgp.WebSocketClient(appView, "notifyEvent");
		websocketClient.initialize();
		appView.getTermData([ wgp.constants.TREE.DATA_ID ], new Date(),
				new Date());

		$("#tree_area")
				.click(
						function() {
							if ($("[id$='mapreduce/task']") != undefined) {

								var elem = $("[id$='mapreduce/task']");

								$("#tree_area").jstree("delete_node",
										elem);
							}
						});
	</script>
	
	<input type="hidden" id="context" value="<%=request.getContextPath()%>" />
</body>
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/resources/css/common.css"
	type="text/css" media="all">
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/resources/plugins/halook/css/halook.css"
	type="text/css" media="all">
</html>