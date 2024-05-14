// INSERT YOUR JS HERE


window.clearValidationError = function(inputElement) {
  // remove any lingering error
  const previousError = inputElement.parentElement.querySelector(".invalid-feedback");
  if(previousError) {
    previousError.remove();
  }
}

window.addValidationError = function(inputElement, error){
  clearValidationError(inputElement);
  inputElement.classList.add('is-invalid');
  // add <span class="invalid-feedback">​error</span>
  const span = document.createElement("span");
  span.classList.add("invalid-feedback");
  span.append(error);
  inputElement.parentElement.append(span);
}

window.requireField = function(name){
  const field = document.getElementById(name);
  clearValidationError(field);
  if(!field.value || field.value.length == 0) {
    addValidationError(field, "must not be blank");
    return Promise.reject("must not be blank");
  } else {
    return Promise.resolve(field.value);
  }
}

window.requireFields = function(...args){
  return Promise.all(args.map(arg => requireField(arg)));
}

