// Función para cambiar el tema
function changeSkin() {
    const skinSelector = document.getElementById('skinSelector');
    const body = document.body;
    const selectedSkin = skinSelector.value;
    
    // Remover todas las clases de temas
    body.classList.remove('neobrutalist-theme', 'glass-theme');
    
    // Aplicar el tema seleccionado
    if (selectedSkin === 'neobrutalist') {
        body.classList.add('neobrutalist-theme');
    } else if (selectedSkin === 'glass') {
        body.classList.add('glass-theme');
    }
    
    // Actualizar la apariencia del selector inmediatamente
    updateSelectorAppearance(selectedSkin);
    
    // Guardar la selección en localStorage
    localStorage.setItem('selectedSkin', selectedSkin);
}

// Función para actualizar la apariencia del selector
function updateSelectorAppearance(theme) {
    // Dejar que el CSS de cada skin gobierne: limpiar estilos inline
    const selector = document.getElementById('skinSelector');
    const selectorContainer = document.querySelector('.skin-selector');
    if (!selector || !selectorContainer) { return; }
    selector.removeAttribute('style');
    selectorContainer.removeAttribute('style');
}

// Cargar el tema guardado al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    const savedSkin = localStorage.getItem('selectedSkin');
    const skinSelector = document.getElementById('skinSelector');
    // Inicializar picker personalizado
    try {
        const picker = document.getElementById('skinPicker');
        const pickerBtn = document.getElementById('skinPickerButton');
        const list = document.getElementById('skinPickerList');
        if (picker && pickerBtn && list && skinSelector) {
            // Marca de mejora para ocultar el select nativo
            picker.parentElement.classList.add('js-enhanced');

            const setPickerUI = (value) => {
                const labelSpan = pickerBtn.querySelector('.skin-picker__label');
                const chip = pickerBtn.querySelector('.skin-chip');
                const selectedLi = list.querySelector(`.skin-option[data-value="${value}"]`);
                if (!selectedLi || !labelSpan || !chip) {
                    return;
                }
                labelSpan.textContent = selectedLi.querySelector('.opt-label')?.textContent || '';
                chip.className = 'skin-chip ' + (value === 'neobrutalist' ? 'chip-neo' : value === 'glass' ? 'chip-glass' : 'chip-elegant');
                list.querySelectorAll('.skin-option').forEach(li => li.setAttribute('aria-selected', li.dataset.value === value ? 'true' : 'false'));
            };

            // Estado inicial
            const initial = savedSkin || skinSelector.value || 'default';
            setPickerUI(initial);

            // Abrir/cerrar
            pickerBtn.addEventListener('click', () => {
                const isOpen = list.hasAttribute('hidden') === false;
                if (isOpen) {
                    list.setAttribute('hidden', '');
                    pickerBtn.setAttribute('aria-expanded', 'false');
                } else {
                    list.removeAttribute('hidden');
                    pickerBtn.setAttribute('aria-expanded', 'true');
                }
            });
            document.addEventListener('click', (e) => {
                if (!picker.contains(e.target)) {
                    list.setAttribute('hidden', '');
                    pickerBtn.setAttribute('aria-expanded', 'false');
                }
            });

            // Selección de opción
            list.querySelectorAll('.skin-option').forEach(li => {
                li.addEventListener('click', () => {
                    const { value } = li.dataset;
                    if (skinSelector.value !== value) {
                        skinSelector.value = value;
                        changeSkin();
                    } else {
                        updateSelectorAppearance(value);
                    }
                    setPickerUI(value);
                    list.setAttribute('hidden', '');
                    pickerBtn.setAttribute('aria-expanded', 'false');
                });
            });

            // Sincronizar cuando cambie el select nativo por otros medios
            skinSelector.addEventListener('change', () => setPickerUI(skinSelector.value));
        }
    } catch (_) { /* no-op */ }
    
    if (skinSelector) {
        if (savedSkin) {
            skinSelector.value = savedSkin;
            changeSkin();
        } else {
            // Asegurar que el selector tenga la apariencia correcta al cargar
            updateSelectorAppearance('default');
        }
    }

    // Idioma: sincronizar con localStorage y URL
    try {
        const url = new URL(window.location.href);
        const urlLang = url.searchParams.get('lang');
        const storedLang = localStorage.getItem('preferredLang');

        if (urlLang) {
            if (storedLang !== urlLang) {
                localStorage.setItem('preferredLang', urlLang);
            }
            updateLangActive(urlLang);
        } else if (storedLang) {
            // Aplicar automáticamente el idioma preferido si la URL no lo tiene
            url.searchParams.set('lang', storedLang);
            window.location.replace(url.toString());
            return;
        } else {
            updateLangActive('es'); // coincide con default del servidor
        }
    } catch (_) {
        // Ignorar errores de URL
    }

    // Mostrar nombre de archivo seleccionado junto al botón personalizado
    const fileInput = document.getElementById('fileInput');
    const fileNameLabel = document.getElementById('fileNameLabel');
    if (fileInput && fileNameLabel) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files.length > 0) {
                fileNameLabel.textContent = this.files[0].name;
            } else {
                // Si no hay archivo, restaurar placeholder traducido si está disponible en atributo data
                const placeholder = fileNameLabel.getAttribute('data-placeholder');
                if (placeholder) {
                    fileNameLabel.textContent = placeholder;
                }
            }
        });
        // Guardar el texto inicial como placeholder
        if (!fileNameLabel.getAttribute('data-placeholder')) {
            fileNameLabel.setAttribute('data-placeholder', fileNameLabel.textContent);
        }
    }
});

// Cambiar idioma preservando otros parámetros de la URL
function setLang(lang) {
    try {
        localStorage.setItem('preferredLang', lang);
        const url = new URL(window.location.href);
        url.searchParams.set('lang', lang);
        window.location.href = url.toString();
    } catch (e) {
        // Fallback simple
        localStorage.setItem('preferredLang', lang);
        window.location.search = '?lang=' + encodeURIComponent(lang);
    }
}

function updateLangActive(lang) {
    const buttons = document.querySelectorAll('.lang-btn');
    buttons.forEach(btn => {
        const isActive = btn.getAttribute('onclick')?.includes("'" + lang + "'");
        if (isActive) {
            btn.classList.add('active');
            btn.setAttribute('aria-pressed', 'true');
        } else {
            btn.classList.remove('active');
            btn.setAttribute('aria-pressed', 'false');
        }
    });
}

// Efectos adicionales para el tema neobrutalista
document.addEventListener('DOMContentLoaded', function() {
    // Agregar efectos de hover mejorados para elementos neobrutalistas
    const elements = document.querySelectorAll('button, .action-button, input[type="file"]');
    
    elements.forEach(element => {
        element.addEventListener('mouseenter', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = 'translate(-2px, -2px)';
            }
        });
        
        element.addEventListener('mouseleave', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = '';
            }
        });
    });
    
    // Agregar efectos hover al selector cuando esté en modo neobrutalista
    const selectorContainer = document.querySelector('.skin-selector');
    const selector = document.getElementById('skinSelector');
    
    if (selectorContainer && selector) {
        selectorContainer.addEventListener('mouseenter', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = 'rotate(-1deg) translate(-2px, -2px)';
                this.style.boxShadow = '6px 6px 0px #000000';
            }
        });
        
        selectorContainer.addEventListener('mouseleave', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                // Limpiar inline para volver al estilo del CSS del tema
                this.style.transform = '';
                this.style.boxShadow = '';
            }
        });
    }
});
