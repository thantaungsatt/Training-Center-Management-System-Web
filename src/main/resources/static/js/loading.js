(() => {
	const overlay = document.getElementById('appLoadingOverlay');
	if (!overlay) return;
	let shown = false;
	const showLoading = () => {
		if (shown) return;
		shown = true;
		overlay.classList.add('is-visible');
		overlay.setAttribute('aria-hidden', 'false');
		document.body.setAttribute('aria-busy', 'true');
	};

	// Applies to search/filter GETs, saves, deletes and multi-step forms.
	document.addEventListener('submit', event => {
		if (event.defaultPrevented) return;
		const form = event.target;
		if (!(form instanceof HTMLFormElement) || form.target === '_blank' || form.dataset.noLoading !== undefined) return;
		showLoading();
	});

	// Applies to same-tab page navigation, pagination and detail/edit links.
	document.addEventListener('click', event => {
		if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
		const link = event.target.closest('a[href]');
		if (!link || link.target === '_blank' || link.hasAttribute('download') || link.dataset.noLoading !== undefined || link.hasAttribute('data-bs-toggle')) return;
		const destination = new URL(link.href, window.location.href);
		if (destination.origin !== window.location.origin || destination.href === window.location.href || (destination.hash && destination.pathname === window.location.pathname && destination.search === window.location.search)) return;
		showLoading();
	});

	window.addEventListener('pageshow', () => {
		shown = false;
		overlay.classList.remove('is-visible');
		overlay.setAttribute('aria-hidden', 'true');
		document.body.removeAttribute('aria-busy');
	});
})();
