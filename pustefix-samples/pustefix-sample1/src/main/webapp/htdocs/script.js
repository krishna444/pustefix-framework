function rexCallback(result, requestId, responseInfo) {
	document.getElementById("rexresult").innerHTML=result;
	document.getElementById("rexjsresult").innerHTML=getSerial();
}
function rexTest() {
	pfx.render("txt/common.xml", "rextest", "", "", rexCallback, null, "1");
}

function searchCallback(result, requestId, responseInfo) {
	document.getElementById("searchresult").innerHTML=result;
}
function doSearch() {
	pfx.renderSubmit(searchCallback, null, "1", document.getElementById("search"));
}