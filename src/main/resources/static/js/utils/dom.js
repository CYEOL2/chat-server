// src/main/resources/static/js/utils/dom.js

export const getElement = (id) => document.getElementById(id);
export const querySelector = (selector) => document.querySelector(selector);
export const querySelectorAll = (selector) => document.querySelectorAll(selector);

export const setDisplay = (element, displayStyle) => {
    if (element) {
        element.style.display = displayStyle;
    }
};

export const setTextContent = (element, text) => {
    if (element) {
        element.textContent = text;
    }
};

export const setDisabled = (element, disabled) => {
    if (element) {
        element.disabled = disabled;
    }
};

export const addClass = (element, className) => {
    if (element) {
        element.classList.add(className);
    }
};

export const removeClass = (element, className) => {
    if (element) {
        element.classList.remove(className);
    }
};

export const toggleClass = (element, className, force) => {
    if (element) {
        element.classList.toggle(className, force);
    }
};

export const setInputValue = (element, value) => {
    if (element) {
        element.value = value;
    }
};

export const getInputValue = (element) => {
    if (element) {
        return element.value.trim();
    }
    return '';
};
