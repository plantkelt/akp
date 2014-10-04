// Retourne la position du curseur dans un champ
function akp_getCaretPos(input) {
	input.focus();
	var caretpos = 0;
	if (document.selection) {
		input.focus();
		var sel = document.selection.createRange();
		sel.moveStart('character', -input.value.length);
		caretpos = sel.text.length;
	} else if (input.selectionStart || input.selectionStart == '0') {
		caretpos = input.selectionStart;
	}
	return (caretpos);
}

// Modification de la position du curseur dans un champ
function akp_setCaretPos(input, pos) {
	input.focus();
	if (document.selection) {
		input.focus();
		var sel = document.selection.createRange();
		sel.moveStart('character', -input.value.length);
		sel.moveStart('character', pos);
		sel.moveEnd('character', 0);
		sel.select();
	} else if (input.selectionStart || input.selectionStart == '0') {
		input.selectionStart = pos;
		input.selectionEnd = pos;
		input.focus();
	}
}

function akp_isWordChar(ch) {
	var WORDCH = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-áàâäéèêëíîïóòôöúùûü";
	var i;
	for (i = 0; i < WORDCH.length; i++)
		if (ch == WORDCH.charAt(i))
			return true;
	return false;
}

// Macro pour la saisie
function akp_surround(input, tag, n) {
	var txt = input.value;
	var cindex = akp_getCaretPos(input);
	var bindex = cindex - 1;
	var eindex = bindex;
	var txt = input.value;
	var n2 = 0;
	while (bindex >= 0) {
		if (!akp_isWordChar(txt.charAt(bindex))) {
			n2++;
			if (n2 >= n)
				break;
		}
		bindex--;
	}
	bindex++;
	while (eindex < txt.length && akp_isWordChar(txt.charAt(eindex)))
		eindex++;
	var a = txt.substr(0, bindex);
	var b = txt.substr(bindex, eindex - bindex);
	var c = txt.substr(eindex, txt.length - eindex);
	txt = a + "<" + tag + ">" + b + "</" + tag + ">" + c;
	input.value = txt;
	akp_setCaretPos(input, cindex + 7);
}

function akp_insert(input, what) {
	var txt = input.value;
	var cindex = akp_getCaretPos(input);
	var a = txt.substr(0, cindex);
	var b = txt.substr(cindex, txt.length - cindex);
	txt = a + "<" + what + ">" + b;
	input.value = txt;
	akp_setCaretPos(input, cindex + 2 + what.length);
}

function akp_submit(submitBtnId) {
	if (submitBtnId != null)
		document.getElementById(submitBtnId).click();
}

function akp_handleMacro(input, ev, submitBtnId) {
	var key;
	if (ev.which)
		key = ev.which;
	else
		key = ev.keyCode;
	if (ev.ctrlKey) {
		switch (key) {
		case 13:
			akp_submit(submitBtnId);
			break;
		}
	}
	if (ev.ctrlKey && ev.shiftKey) {
		switch (key) {
		// case 65:
		case 90:
			akp_surround(input, "a", 1);
			break;
		case 66:
			akp_surround(input, "b", 2);
			break;
		case 69:
			akp_surround(input, "e", 1);
			break;
		// case 73:
		case 75:
			akp_surround(input, "i", 2);
			break;
		case 76:
			akp_insert(input, "l");
			break;
		case 58:
		case 59:
		case 186:
			akp_insert(input, "/l");
			break;
		}
	}
	return false;
}