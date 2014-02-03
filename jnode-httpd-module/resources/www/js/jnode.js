/**
 * 
 * @author kreon
 * 
 * Some functions were taken from stackoverflow.com
 */


function _fill(name, val) {
	var _var = document.getElementById(name);
	if (_var) {
		if (_var.innerHTML) { // add
			_var.innerHTML += val;
		} else {
			_var.innerHTML = val;
		}
	}
}

function _clear(name) {
	var _var = document.getElementById(name);
	if (_var) {
		if (_var.innerHTML) { // add
			_var.innerHTML = "";
		}
	}
}

function _value(name, value) {
	var _var = document.getElementById(name);
	if (_var) {
		_var.value = value;
	}
}

function _var(name) {
	var _var = document.getElementById(name);
	if (_var) {
		return _var.value;
	} else {
		return null;
	}
}

function _getXmlHttp() {
	var xmlhttp;
	try {
		xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
	} catch (e) {
		try {
			xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		} catch (E) {
			xmlhttp = false;
		}
	}
	if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
		xmlhttp = new XMLHttpRequest();
	}
	return xmlhttp;
}

function _get(url) {
	var xmlhttp = _getXmlHttp()
	xmlhttp.open('GET', url, false);
	xmlhttp.send(null);
	if (xmlhttp.status == 200) {
		return xmlhttp.responseText;
	} else {
		return "";
	}
}
function _jrun(url, callback) {
	url += ((url.indexOf("?") >= 0) ? "&cb=" : "?cb=") + callback;
	eval(_get(url));
}

function _getParameterByName(name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regex = new RegExp("[\\?&#]" + name + "=([^&#]*)"), results = regex
			.exec(document.location.href);
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g,
			" "));
}