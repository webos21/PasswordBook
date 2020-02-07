export default {
  items: [
    {
      name: 'Dashboard',
      url: '/dashboard',
      icon: 'icon-speedometer'
    },
    {
      name: 'PasswordBook',
      url: '/passwordbook',
      icon: 'icon-key',
    },
    {
      name: 'Download App',
      url: 'https://webos21.github.io/PasswordBook/',
      icon: 'icon-cloud-download',
      class: 'mt-auto',
      variant: 'success',
      attributes: { target: '_blank', rel: "noopener" },
    },
    {
      name: 'Logout',
      url: '/logout',
      icon: 'icon-lock',
      variant: 'danger',
    },
  ],
};
