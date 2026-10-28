import React from 'react';
import { useT } from '../i18n/languageContext.js';

const CustomerProfilePage = () => {
    const { t } = useT();
    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <h2>{t('Customer Profile & Feedback')}</h2>
            <br />
            {/* Profile Settings */}
            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
                <h3>{t('Profile Settings')}</h3>
                <div style={{ margin: '15px 0' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>{t('Profile Picture:')}</label>
                    <input type="file" />
                </div>
            </div>

            {/* Payment & Service History */}
            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
                <h3>{t('Service & Payment History')}</h3>
                <p style={{ color: 'green', fontWeight: 'bold', margin: '10px 0' }}>{'\u2714'} {t('Last Payment')}: {'\u09F3'}500 ({t('Successful')})</p>
            </div>

            {/* Review Form */}
            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginBottom: '20px' }}>
                <h3>{t('Submit Review')}</h3>
                <div style={{ margin: '10px 0' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>{t('Rating (1-5):')}</label>
                    <input type="number" min="1" max="5" defaultValue="5" style={{ padding: '8px', width: '60px' }} />
                </div>
                <div style={{ margin: '10px 0' }}>
                    <textarea placeholder={t('Write your review...')} style={{ width: '100%', height: '80px', padding: '8px' }}></textarea>
                </div>
                <button style={{ padding: '8px 16px', background: '#007bff', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>{t('Submit Review')}</button>
            </div>

            {/* Complaint Form */}
            <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                <h3>{t('Lodge Complaint')}</h3>
                <textarea placeholder={t('Describe your issue or complaint...')} style={{ width: '100%', height: '80px', padding: '8px', margin: '10px 0' }}></textarea>
                <button style={{ padding: '8px 16px', background: '#dc3545', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>{t('Submit Complaint')}</button>
            </div>
        </div>
    );
};

export default CustomerProfilePage;
